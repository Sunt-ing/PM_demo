# To developers

change list:
2021-11-14  游正新 孙挺
端口号改动为 23332 和 23333
前端api的申请地址，部署后需要写localhost
node版本在linux上需要用10.22.0。但是后端init中，FastJson处理部分会出现问题（用时不正常）。
故暂时使用windows启动

运行方式：
进入我们的总目录PM_demo：cd PM_demo
运行jar包：nohup java -jar target/PatternMiningDemo-0.0.1-SNAPSHOT.jar &
进入前端目录：cd target
运行前端服务：npm start


