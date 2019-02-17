package com.itheima.rpc

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

class Master extends Actor{
  //构造函数时先运行
  println("Master 构造器被调用")

  //在构造函数执行完成后被调用,只执行一次
  override def preStart() ={
    println("prestart方法执行了")
  }
  //receive在prestart方法执行完成后被调用,会一直接受消息
  override def receive: Receive = {
    case "connect" => {
      println("客户端被连接了...");
      sender() ! "success";
    }

  }

}
object Master{
  def main(args: Array[String]): Unit = {
    //需要先获取得到actor的老大
    //1.创建ActorSystem,负责创建监督actor

    //master ip地址和端口
    val host = args(0);
    val port = args(1)



    //准备配置对象所需要的字符串(11:58)
    //stripMargin方法就是按|进行切分
    val configStr =
    s"""
        |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
        |akka.remote.netty.tcp.hostname = "$host"
        |akka.remote.netty.tcp.port = "$port"
      """.stripMargin
    val config = ConfigFactory.parseString(configStr);
    val masterActorSystem = ActorSystem("masterActorSystem", config)
    //2.创建master actor
    val masterActors: ActorRef = masterActorSystem.actorOf(Props(new Master), "masterActor")

    //3.测试 向master acotr发送消息
//    masterActors ! "connect";


  }
}
