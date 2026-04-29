/**
 * 本模块用于解析ws消息
 */
const pako = require("pako");
require("@/lib/douyinProto.js");

/**
 * 返回聊天类型的消息内容
 */
const getOneChatMsgContent = (message) => {
    let method = message.getMethod();
    let data;
    let chat = {
        id: 0,
        type: undefined,
        nickname: "",
        content: "",
        memberCount: 0,
        likeCount: 0,
        followCount: 0,
        totalUserCount: 0,
        rank: [],
        gift: {
            name: "",
            count: 0,
            url: "",
            desc: "",
        },
    };
    switch (method) {
        // 进入
        case "WebcastMemberMessage":
            data = proto.MemberMessage.deserializeBinary(message.getPayload());
            // membercount - 直播间人数， user.nickname
            chat.type = "member";
            chat.nickname = data.getUser().getNickname();
            chat.content = "来了";
            chat.memberCount = data.getMembercount();
            break;
        // 关注
        case "WebcastSocialMessage":
            data = proto.SocialMessage.deserializeBinary(message.getPayload());
            chat.type = "social";
            chat.nickname = data.getUser().getNickname();
            chat.content = data.getUser().getNickname() + ":关注了主播";
            chat.followCount = data.getFollowcount();
            break;
        // 聊天
        case "WebcastChatMessage":
            data = proto.ChatMessage.deserializeBinary(message.getPayload());
            chat.type = "chat";
            chat.nickname = data.getUser().getNickname();
            chat.content = data.getContent();
            break;
        // 点赞
        case "WebcastLikeMessage":
            data = proto.LikeMessage.deserializeBinary(message.getPayload());
            chat.type = "like";
            chat.nickname = data.getUser().getNickname();
            chat.content = data.getUser().getNickname() + ":为主播点赞了";
            chat.likeCount = data.getTotal();
            break;
        // 礼物
        case "WebcastGiftMessage":
            data = proto.GiftMessage.deserializeBinary(message.getPayload());
            chat.type = "gift";
            chat.nickname = data.getUser().getNickname();
            chat.content = data.getCommon().getDescribe();
            chat.gift.name = data.getGift().getName();
            chat.gift.desc = data.getGift().getDescribe();
            chat.gift.count = data.getRepeatcount();
            chat.gift.diamondCount = data.getGift().getDiamondcount();
            chat.gift.url = data.getGift().getImage().getUrllistList()[0];
            chat.gift.repeatEnd = data.getRepeatend();
            break;
        // 直播间统计
        // case "WebcastRoomUserSeqMessage":
        //     data = proto.RoomUserSeqMessage.deserializeBinary(message.getPayload());
        //     chat.type = "room";
        //     chat.content = "";
        //     chat.memberCount = data.getTotal();
        //     chat.totalUserCount = data.getTotaluser();
        //     let rank = data.getRanksList();
        //     for (let item of rank) {
        //         let rankItem = {
        //             nickname: item.getUser().getNickname(),
        //             avatar: item.getUser().getAvatarthumb().getUrllistList()[0],
        //             rank: item.getRank(),
        //         };
        //         chat.rank.push(rankItem);
        //     }
        //     break;
    }
    chat.id = data?.getCommon().getMsgid();
    return chat;
}

/**
 * 解析抖音ws消息
 * 返回一组聊天类型的消息内容 Array<string>
 */
const parseDouyinMsg = (originMsg) => {
    const frame = proto.PushFrame.deserializeBinary(originMsg);
    // 对PushFrame的 payload 内容进行gzip解压
    let headers = frame.getHeaderslistMap().map_;
    let gzipFlag = false;
    for (const t of Object.values(headers)) {
        if ("compress_type" === t.key && "gzip" === t?.value) {
            gzipFlag = true;
        }
    }
    const payload = gzipFlag
        ? pako.inflate(frame.getPayload())
        : frame.getPayload_asU8();

    // 根据Response+gzip解压数据，生成数据对象
    const res = proto.Response.deserializeBinary(payload);

    if (res.getNeedack()) {
        let ext = res.getInternalext();
        const sf = new proto.PushFrame();
        sf.setPayloadtype("ack");
        sf.setPayload(
            (function (e) {
                const t = [];
                for (const i of e) {
                    const e = i.charCodeAt(0);
                    e < 128
                        ? t.push(e)
                        : e < 2048
                            ? (t.push(192 + (e >> 6)), t.push(128 + (63 & e)))
                            : e < 65536 &&
                            (t.push(224 + (e >> 12)),
                                t.push(128 + ((e >> 6) & 63)),
                                t.push(128 + (63 & e)));
                }
                return Uint8Array.from(t);
            })(ext)
        );
        sf.setLogid(frame.getLogid());

        // 原生websocket需要给抖音发送心跳才能一直收到数据推送
        // ws.send(sf.serializeBinary());
    }

    return res.getMessagesList().map((message) => getOneChatMsgContent(message)).filter(Boolean);
};


module.exports = parseDouyinMsg;
