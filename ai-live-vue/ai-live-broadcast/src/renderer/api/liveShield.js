import request from '@/utils/request'

//保存屏蔽自己&敏感词
export function save (data) {
    return request({
        url: '/appc/live-shield/save',
        method: 'post',
        data
    })
}
//获取商户屏蔽自己&敏感词
export function getByMerchantId () {
    return request({
        url: '/appc/live-shield/get-by-merchant',
        method: 'post'
    })
}
