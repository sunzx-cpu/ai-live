import request from '@/utils/request'

//保存直播间公屏配置
export function save (data) {
    return request({
        url: '/appc/live-public-screen-config/save',
        method: 'post',
        data
    })
}
//查询商户直播间公屏配置列表
export function getByMerchantId () {
    return request({
        url: '/appc/live-public-screen-config/get-by-merchant',
        method: 'post'
    })
}
