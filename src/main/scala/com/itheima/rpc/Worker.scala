package com.itheima.rpc

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

//todo:利用akka模型,实现两个进程间的通信
class Worker extends Actor{

  override def preStart()={
   //获取Master引用
    //通过ActorContext上下文对象,调用actorSelection方法,需要一个字符串,这个字符串就封装了master的一些条件
    //条件有:akka.tcp://masterActorSystem@127.0.0.1:8888/user/masterAcotr
    //1.通信协议
    //2.IP地址 端口号
    //3.创建master的老大
    //4.层级关系
    //5.master actor的名称
    val master: ActorSelection = context.actorSelection("akka.tcp://masterActorSystem@127.0.0.1:8888/user/masterActor")
    //通过master想masteractor发送消息
    master ! "connect";
  }
  override def receive: Receive = {
    case "connect" => println("连接上..");
    case "success" => println("worker接收master回应的信息");
  }
}

object Worker{
  def main(args: Array[String]): Unit = {

    //master ip地址和端口
    val host = args(0);
    val port = args(1)

    val configStr =
      s"""
        |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
        |akka.remote.netty.tcp.hostname = "$host"
        |akka.remote.netty.tcp.port = "$port"
      """.stripMargin

    val config = ConfigFactory.parseString(configStr);
    val workerActorSystem = ActorSystem("workerActorSystem", config)
    val workerActor: ActorRef = workerActorSystem.actorOf(Props(new Worker), "WorkerActor")
//    workerActor ! "connect";
  }
}

