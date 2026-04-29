import {defineStore} from 'pinia'
import {resetRouter} from '@/router'
import {usePermissionStore} from './permission'
import {getInfo, login} from '../api/login'
import {Message} from "element-ui";
import {ipcRenderer} from "electron";

const store = () => {
    return {
        token: JSON.parse(localStorage.getItem('token')),
        name: JSON.parse(localStorage.getItem('name')),
        userId: JSON.parse(localStorage.getItem('userId')),
        roles: JSON.parse(localStorage.getItem('roles'))
    }
}

export const useUserStore = defineStore({
    id: 'user',
    store,
    actions: {
        login(data) {
            return new Promise((resolve, reject) => {
                login(data).then(res => {
                    if (res.data && res.data.code === 0) {
                        localStorage.setItem("token", res.data.token);
                        ipcRenderer.invoke("save-token", res.data.token);
                        this.token = res.data.token;
                    } else {
                        Message.error(res.data.msg)
                    }
                    resolve()
                })
            })

        },
        logOut() {
            return new Promise((resolve, reject) => {
                const {ResetRoutes} = usePermissionStore()
                localStorage.setItem("token", "");
                localStorage.setItem("roles", JSON.stringify([]));
                localStorage.setItem("name", "");
                this.token = ""
                this.name = ""
                this.roles = []
                ResetRoutes()
                resetRouter()
                resolve()
            })
        },
        GetUserInfo() {
            return new Promise((resolve, reject) => {
                getInfo().then(res => {
                    if (res.data && res.data.code === 0) {
                        localStorage.setItem("name", res.data.user.username);
                        localStorage.setItem("userId", res.data.user.id);
                        localStorage.setItem("roles", JSON.stringify(["admin"]));
                        this.name = res.data.user.username
                        this.userId = res.data.user.id;
                        this.roles = ["admin"]
                    } else {
                        Message.error(res.data.msg)
                    }
                    resolve(this.roles)
                })
            })
        }
    }
})