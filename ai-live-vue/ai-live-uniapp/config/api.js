const http = uni.$u.http;

// ==================== 用户认证相关 ====================
// 登录
const login = (params) => http.post('/appc/login', params)
// 注册
const register = (params) => http.post('/appc/register', params)
// 发送手机验证码
const sendSms = (params) => http.post('/appc/user/sendSms', params)
// 用户信息
const userProfile = (params) => http.post('/appc/user/info', params)
// 修改密码
const userChangePassword = (params) => http.post('/app/user/changePassword', params)
// 忘记密码
const userForgetPassword = (params) => http.post('/app/user/forgetPassword', params)
// 申请注销账户
const userCancellationAccount = (params) => http.post('/app/user/cancellationAccount', params)

// ==================== 验证码相关 ====================
// 忘记密码验证码
const codeForgetPassword = (params) => http.post('/app/code/forgetPassword', params)
// 验证忘记密码验证码
const codeVerifyForgetPassword = (params) => http.post('/app/code/verifyForgetPassword', params)

// ==================== App 更新相关 ====================
// 获取安卓最新更新信息
const appversion_android = (params = {}) => http.post("/app/appversion/android", params);

// ==================== 文件上传 ====================
// 上传文件（通用）
const uploadFile = (
	file,
	formData = {},
	url = http.config.baseURL+'/app/oss/upload',
) => new Promise((resolve, reject) => {
	uni.uploadFile({
		url: url,
		filePath: file,
		name: 'file',
		formData: formData,
		success: (res) => {
			resolve(res?.data?JSON.parse(res.data):res)
		},
		fail:(err)=>{
			reject(err)
		}
	})
});

export default {
  // 用户认证
  login,
  register,
  sendSms,
  userProfile,
  userChangePassword,
  userForgetPassword,
  userCancellationAccount,

  // 验证码
  codeForgetPassword,
  codeVerifyForgetPassword,

  // App 更新
  appversion_android,

  // 文件上传
  uploadFile,
}
