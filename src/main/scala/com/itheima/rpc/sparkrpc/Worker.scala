package com.itheima.rpc.sparkrpc

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
//导入时间单位的包
import scala.concurrent.duration._

class Worker(val memory:Int,val cores:Int,val masterIP:String,val masterPort:String) extends Actor{

  private val workerId: String = UUID.randomUUID().toString

//定义发送心跳的时间间隔
  val sendHeatBeatInterval = 100000;
  //表示给一个默认初始值 null
  var master: ActorSelection = _;

  override def preStart()={
    //获取Master引用
    //通过ActorContext上下文对象,调用actorSelection方法,需要一个字符串,这个字符串就封装了master的一些条件
    //条件有:akka.tcp://masterActorSystem@127.0.0.1:8888/user/masterActor
    //1.通信协议
    //2.IP地址 端口号
    //3.创建master的老大
    //4.层级关系
    //5.master actor的名称
    master = context.actorSelection(s"akka.tcp://masterActorSystem@$masterIP:$masterPort/user/masterActor")
    //通过master想masteractor发送消息,通过样例类封装注册信息(workerId,memory,cores)
    master ! RegisterMessage(workerId,memory,cores)
  }
  override def receive: Receive = {
    //接收到注册成功的信息
    case RegisteredMessage(message) => {
      println(message)
      //定时发送心跳
      //self表示本身(15:50) 由于master类型和需要的参数类型不符合,所以不可以直接向master发送消息,这里用self表示worker本身
      //需要手动导入一个隐式转换
      import context.dispatcher
      //利用akka框架的定时调度发送
      //第三个参数因为参数需要的类型是ActorRef,而master类型是ActorSelection不匹配,所以需要self表示worker本身
      context.system.scheduler.schedule(0 millis,sendHeatBeatInterval millis,self,SendHeatBeat);
    };
    //worker向自身发送信息
    case SendHeatBeat => {
      //worker真正向master发送信息
      master ! HeatBeat(workerId);
    }
  }
}

object Worker{
  def main(args: Array[String]): Unit = {

    //master ip地址和端口
    val host = args(0);
    val port = args(1)

    //定义worker的内存和核数
    val memory = args(2).toInt
    val cores = args(3).toInt
    //得到master的ip和端口
    val masterIP = args(4)
    val masterPort = args(5)

    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
      """.stripMargin

    val config = ConfigFactory.parseString(configStr);
    val workerActorSystem = ActorSystem("workerActorSystem", config)
    val workerActor: ActorRef = workerActorSystem.actorOf(Props(new Worker(memory,cores,masterIP,masterPort)), "WorkerActor")
    //    workerActor ! "connect";
  }
}
