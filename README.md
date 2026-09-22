# 校园生活服务管理（Android + Flask 服务端 + 移动 Web）

校园生活服务管理系统的完整实现：Android 客户端调用 Flask 提供的 JSON API，同一服务还内置了一个浏览器端的管理页面。

功能模块：用户认证、快递服务、饮食订餐、热水服务、打印服务、上网服务、钱包。

## 组成

| 目录 | 说明 |
| --- | --- |
| `app/` | Android 客户端（Java，compileSdk 34 / minSdk 21，Gradle 7.3.3，AGP 7.2.2） |
| `server/` | Flask 服务端，数据以 JSON 文件存放在 `server/data/` |

## 启动服务端

```bash
cd server
pip install -r requirements.txt      # flask==3.0.0, flask-cors==4.0.0
python app.py                        # http://0.0.0.0:5000
```

- 管理页面：`http://localhost:5000/`
- 示例账号：`admin` / `demo1234`

## 启动客户端

用 Android Studio 打开项目根目录，Gradle Sync 后 Run。

模拟器访问宿主机使用默认地址 `http://10.0.2.2:5000`（见 `HttpUtil.BASE_URL`）。真机需把该常量改成电脑的局域网 IP：

```java
// app/src/main/java/cn/edu/gdpnc/jsjxy/mmt/HttpUtil.java
public static String BASE_URL = "http://10.0.2.2:5000";
```

## 说明

- 本仓库只包含源码，课程设计报告文档与演示视频未纳入版本管理。
- `server/data/*.json` 中的姓名、学号、手机号等示例数据已脱敏。
