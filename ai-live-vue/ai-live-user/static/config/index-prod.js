/**
 * 生产环境
 */
;(function () {
  window.SITE_CONFIG = {};

  // api接口请求地址
  window.SITE_CONFIG['baseUrl'] = 'http://117.72.100.80:8201/ai-live-api';
  // 前端地址
  window.SITE_CONFIG['webUrl'] = 'http://117.72.100.80:8200';
  // 音频接口地址
  window.SITE_CONFIG['audioUrl'] = 'http://127.0.0.1:8205';

  // cdn地址 = 域名 + 版本号
  window.SITE_CONFIG['domain']  = './'; // 域名
  window.SITE_CONFIG['version'] = '';   // 版本号(年月日时分)
  window.SITE_CONFIG['cdnUrl']  = window.SITE_CONFIG.domain + window.SITE_CONFIG.version;
})();
