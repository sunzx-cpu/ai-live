/**
 * 本模块用于获取直播间信息、ws地址
 */

const signature = require("@/lib/douyinSignature.js");
const axios = require("axios");
const log = require('electron-log')
const DouyinService = require("@/utils/douyinService");
const gptService = require('@/utils/gptService')
const knowledgeBase = require('@/utils/knowledgeBase')
const {ipcRenderer} = require("electron");


/**
 * 根据liveId获取直播房间信息
 */
async function fetchLiveRoomInfo(liveUrl) {
    try {
        const res = await axios.get(liveUrl, {
            headers: {},
        });
        let html = res.data;
        const matchRes = html.match(
            /<script\snonce="\S+?"\s>self\.__pace_f\.push\(\[1,"[a-z]?:\[\\"\$\\",\\"\$L\d+\\",null,([\s\S]+?)\]\\n"\]\)<\/script>/
        );
        const REGLIST = [
            {
                reg: /\\{1,7}"/g,
                str: '"',
            },
            {
                reg: /"\{/g,
                str: "{",
            },
            {
                reg: /\}"/g,
                str: "}",
            },
            {
                reg: /"\[/g,
                str: "[",
            },
            {
                reg: /\]"/g,
                str: "]",
            },
        ];
        if (!matchRes) throw new Error("房间信息获取失败");
        let json = matchRes[1];
        for (const REG of REGLIST) {
            json = json.replace(REG.reg, REG.str);
        }
        const dict = JSON.parse(json);
        let roomId = dict["state"]["roomStore"]["roomInfo"]["roomId"];
        let roomTitle = dict["state"]["roomStore"]["roomInfo"]["room"]["title"];
        let roomUserCount =
            dict["state"]["roomStore"]["roomInfo"]["room"]["user_count_str"];
        let uniqueId = dict["state"]["userStore"]["odin"]["user_unique_id"];

        return {roomId, roomTitle, roomUserCount, uniqueId};
    } catch (error) {
        console.error(error);
    }
}

/**
 * 获取ws地址
 */
const getDyWsUrl = (roomId, uniqueId) => {
    const sign = signature.getSign(roomId, uniqueId)["X-Bogus"];
    const now = Date.now();
    const wsUrl = `wss://webcast100-ws-web-lq.douyin.com/webcast/im/push/v2/?app_name=douyin_web&version_code=180800&webcast_sdk_version=1.3.0&update_version_code=1.3.0&compress=gzip&internal_ext=internal_src:dim|wss_push_room_id:${roomId}|wss_push_did:${uniqueId}|fetch_time:${now}|seq:1|wss_info:0-${now}-0-0&cursor=t-${now}_r-1_d-1_u-1_h-1&host=https://live.douyin.com&aid=6383&live_id=1&did_rule=3&debug=false&maxCacheMessageNumber=20&endpoint=live_pc&support_wrds=1&im_path=/webcast/im/fetch/&user_unique_id=${uniqueId}&device_platform=web&cookie_enabled=true&screen_width=1920&screen_height=1080&browser_language=zh-CN&browser_platform=Win32&browser_name=Mozilla&browser_version=5.0%20(Windows%20NT%2010.0;%20Win64;%20x64)%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/111.0.0.0%20Safari/537.36%20Edg/111.0.1661.62&browser_online=true&tz_name=Asia/Shanghai&identity=audience&room_id=${roomId}&heartbeatDuration=0&signature=${sign}`;
    return wsUrl;
}

/**
 * 屏蔽用户敏感词
 *
 * @param msg
 */
async function filterMsg(msg) {
    // 弹幕屏蔽的用户昵称
    let username = (localStorage.getItem('shield-username') || "").split("|").filter(word => word.trim());
    // 移除用户名字或发言中的字词, 如名字前的灯牌名或违禁词
    let sensitiveWords = (localStorage.getItem('shield-sensitiveWords') || "").split("|").filter(word => word.trim());
    //如果用户名字或发言中的字词包含了设置的字词，则整个屏蔽
    let wholeSentence = (localStorage.getItem('shield-wholeSentence') || "").split("|").filter(word => word.trim());

    // 应用屏蔽规则
    try {
        // 1. 昵称中的用户名和敏感词替换
        const nicknameReplacer = replaceWithPattern([...username, ...sensitiveWords]);
        msg.nickname = nicknameReplacer(msg.nickname);

        // 2. 内容中的敏感词替换
        const contentReplacer = replaceWithPattern(sensitiveWords);
        msg.content = contentReplacer(msg.content);

        // 3. 整句屏蔽检查（如果内容包含wholeSentence中的任何词，整条消息被屏蔽）
        const shouldBlock = wholeSentence.some(keyword =>
            msg.content.includes(keyword) || msg.nickname.includes(keyword)
        );

        if (shouldBlock) {
            msg.nickname = "***";
            msg.content = "***";
        }
    } catch (error) {
        console.error("弹幕过滤处理出错:", error);
    }
    return msg
}

// 构建替换函数
function replaceWithPattern(words, replacement = "***") {
    if (!words || words.length === 0) return msg => msg; // 无屏蔽词时返回原消息

    const pattern = words.join("|");
    if (!pattern) return msg => msg;

    return function (text) {
        return text.replace(new RegExp(pattern, "g"), replacement);
    };
}

/**
 * 开启文本回复
 * @param msg
 * @param webview
 */
function enableTextReply(msg, webview) {
    // 获取关键词列表
    let keywords = JSON.parse(localStorage.getItem("interactKeyword"))
    if (keywords.length > 0) {
        keywords.forEach(keyword => {
            // 存在关键词则回复
            if (keyword.keyword.split("|").includes(msg.content)) {
                // 发送弹幕
                const douyinService = new DouyinService()
                douyinService.sendDanmaku(keyword.content, webview);
            }
        })
    }
}

/**
 * 开启GPT互动（优先级低于关键词互动）
 */
async function enableGPTInteractive(userMessages, webview) {
    for (let userMessage of userMessages) {
        const interactionEnabled = JSON.parse(localStorage.getItem('gptInteractionEnabled'))
        if (interactionEnabled) {
            const knowledgeEnabled = JSON.parse(localStorage.getItem('gptKnowledgeEnabled'))
            const repeatUserContent = JSON.parse(localStorage.getItem('gptRepeatUserContent'))
            const repeatUserContentPrefix = JSON.parse(localStorage.getItem('gptRepeatUserContentPrefix'))
            const sayUserName = JSON.parse(localStorage.getItem('gptSayUserName'))
            const sayUserNamePrefix = JSON.parse(localStorage.getItem('gptSayUserNamePrefix'))
            const interactionTipWord = localStorage.getItem('gptInteractionTipWord')
            const interactNow = localStorage.getItem('gptInteractNow')
            try {
                // 构建上下文信息
                let contextInfo = ''

                // 是否加载知识库信息
                if (knowledgeEnabled) {
                    if (knowledgeBase.isKnowledgeLoaded()) {
                        // 尝试从知识库中搜索相关信息
                        const searchResults = knowledgeBase.search(userMessage.content)
                        if (searchResults.length > 0) {
                            contextInfo += knowledgeBase.formatForPrompt(searchResults.slice(0, 2))  // 最多2个结果
                        }
                    }
                }

                // 调用 GPT 生成回复
                const result = await gptService.replyToKeyword(userMessage.content, contextInfo, interactionTipWord)

                if (result.success) {
                    let replyText = ''

                    // 是否重复用户发言
                    if (repeatUserContent) {
                        // 随机选择一个
                        const split = repeatUserContentPrefix.split('|');
                        const randomElement = split[Math.floor(Math.random() * split.length)];
                        replyText += randomElement + userMessage.content + '，'
                    }

                    // 是否点用户名字
                    if (sayUserName && userMessage.nickname) {
                        // 随机选择一个
                        const split = sayUserNamePrefix.split('|');
                        const randomElement = split[Math.floor(Math.random() * split.length)];
                        replyText += userMessage.nickname + randomElement + '，'
                    }

                    // 加上 GPT 回复
                    replyText += result.content

                    if (replyText) {
                        // 插入到播放队列（使用临时话术的方法）
                        ipcRenderer.send("send-temporary-speech", replyText)
                    }

                    if (interactNow) {
                        // 打断立即开始互动
                        ipcRenderer.send("send-interact-now")
                    }

                    // 发送弹幕
                    const douyinService = new DouyinService()
                    douyinService.sendDanmaku(result.content, webview)
                } else {
                    log.info('[LivePane] GPT互动回复失败:', result.error)
                }
            } catch (error) {
                log.info('[LivePane] GPT互动回复异常:', error)
            }
        }
    }
}

module.exports = {
    fetchLiveRoomInfo,
    getDyWsUrl,
    filterMsg,
    enableTextReply,
    enableGPTInteractive
}