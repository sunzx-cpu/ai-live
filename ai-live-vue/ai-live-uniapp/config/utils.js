// 提示框
const showToast = (title,callback,duration=1500,mask=false,position="bottom") => {
	uni.showToast({
		title,duration,position,mask,
		icon: "none",
		complete:()=>{
			if(callback){
				setTimeout(()=>{
					callback();
				},duration);
			}
		}
	});
}
// 成功提示框
const showToastSuc = (title,callback,duration=1500,mask=false,position="bottom") => {
	uni.showToast({
		title,
    duration,
    position,
    mask,
		icon:"success",
		complete:()=>{
			if(callback){
				setTimeout(()=>{
					callback();
				},duration);
			}
		}
	});
}
// 确定模态框(没有取消)
const submitModal = (title,content,callback) => {
	uni.showModal({
		title,
    content,
    cancelText: 'NO',
    confirmText: 'YES',
		showCancel: false,
		success: function (res) {
			if (res.confirm) {
				if(callback){
					callback();
				}
			}
		}
	});
}

// 模态框
const showModal = (title,content,callback) => {
	uni.showModal({
		title,
    content,
    cancelText: 'NO',
    confirmText: 'YES',
		showCancel: true,
		success: function (res) {
			if (res.confirm) {
				if(callback){
					callback();
				}
			}
		}
	});
}

//四舍五入保留2位小数（不够位数，则用0替补）
const keepTwoDecimalFull = (num) => {
	var result = parseFloat(num);
	if (isNaN(result)) {
		return false;
	}
	result = Math.round(num * 100) / 100;
	var s_x = result.toString();
	var pos_decimal = s_x.indexOf('.');
	if (pos_decimal < 0) {
		pos_decimal = s_x.length;
		s_x += '.';
	}
	while (s_x.length <= pos_decimal + 2) {
		s_x += '0';
	}
	return s_x;
}

const isLogin = (vue) => {
  return !!vue.vuex_token;
}

const getPage = (level=1)=>{
	const pages = getCurrentPages();
	return pages[pages.length - level];
}

const copyTxt = (txt)=>{
	return new Promise(function(resolve){
		txt+="";
		if(txt=="null"||txt=="undefined"||txt==""){
			return;
		}
		let textarea = document.createElement("textarea")
		textarea.value = txt
		textarea.readOnly = "readOnly"
		document.body.appendChild(textarea)
		textarea.select() // 选中文本内容
		textarea.setSelectionRange(0, txt.length) 
		let result = document.execCommand("copy") 
		textarea.remove()
		resolve();
	});
}

const strDate = (udate,format="yyyy/MM/dd hh:mm:ss") => {
    if (udate) {
        udate=parseInt(udate);
        var newDate = new Date(udate)
    } else {
        var newDate = new Date()
    }
    var date = {
       "M+": newDate.getMonth() + 1,
       "d+": newDate.getDate(),
       "h+": newDate.getHours(),
       "m+": newDate.getMinutes(),
       "s+": newDate.getSeconds(),
       "q+": Math.floor((newDate.getMonth() + 3) / 3),
       "S+": newDate.getMilliseconds()
    };
    if (/(y+)/i.test(format)) {
       format = format.replace(RegExp.$1, (newDate.getFullYear() + '').substr(4 - RegExp.$1.length));
    }
    for (var k in date) {
       if (new RegExp("(" + k + ")").test(format)) {
           format = format.replace(RegExp.$1, RegExp.$1.length == 1
              ? date[k] : ("00" + date[k]).substr(("" + date[k]).length));
       }
    }
    return format;
}

export default {
	showToast,
	showToastSuc,
	submitModal,
	showModal,
	getPage,
	isLogin,
	copyTxt,
	keepTwoDecimalFull,
  strDate,
}