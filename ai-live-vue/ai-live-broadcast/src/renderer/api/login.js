import request from '@/utils/request'

export function login (data) {
  return request({
    url: '/appc/login',
    method: 'post',
    data
  })
}

export function getInfo () {
  return request({
    url: '/appc/user/info',
    method: 'post',
  })
}

export function message () {
  return request({
    url: '/message',
    method: 'get'
  })
}
