package com.itheima.rpc.sparkrpc

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class Master extends Actor{

  //定义一个map结合,存储worker的注册信息
  private val workerInfoMap = new mutable.HashMap[String,WorkerInfo]()
  //定义一个list集合,存储workerInfo信息,以资源大小排序,以便分配资源
  private val workerInfoList = new ListBuffer[WorkerInfo]
  //定义master定时检查的时间间隔15s
  val checkOutTimeInterval = 15000

  override def preStart(): Unit = {
    //定时检查超时worker
    //需要手动导入一个隐式转换
    import context.dispatcher
    context.system.scheduler.schedule(0 millis,checkOutTimeInterval millis,self,CheckTimeOut)
  }

  override def receive: Receive = {
    //接收worker的注册信息
    case RegisterMessage(workerId,memory,cores) =>{
      //判断没有注册过的worker信息,master只接收没有注册的worker信息
      if(!workerInfoMap.contains(workerId)){
        //map中不包含workid
        val workerInfo = new WorkerInfo(workerId, memory, cores)
        workerInfoMap.put(workerId,workerInfo);
        workerInfoList += workerInfo;
        //master向worker反馈注册成功消息给worker
        sender ! RegisteredMessage(s"WorkerId:$workerId 注册成功!")
      }
    }
    case HeatBeat(workerId) =>{
      //判断当前worker是否注册,master只接受已经注册过的worker信息
      if(workerInfoMap.contains(workerId)){
        //判断worker是否超时
        val workerInfo: WorkerInfo = workerInfoMap(workerId)
        //获取系统当前时间
        val lastTime: Long = System.currentTimeMillis()
        workerInfo.lastHeartBeatTime = lastTime;
      }
    }
      //接收自己的消息
    case CheckTimeOut =>{
      //判断超时逻辑:当前时间-worker上一次心跳时间>master定时检查的时间间隔

      val currentTime: Long = System.currentTimeMillis();
      val outTimeWorkerInfoList: ListBuffer[WorkerInfo] = workerInfoList.filter(currentTime - _.lastHeartBeatTime > checkOutTimeInterval)

      for(c <- outTimeWorkerInfoList){
        val id: String = c.workId
        workerInfoMap.remove(id);
        workerInfoList -= c;

        println(s"workerID$id 已经超时");
      }
      println("活着的worker数:"+workerInfoList.size)

      //按照worker的内存大小降序排列
      println(workerInfoList.sortBy(_.memory).reverse)
    }
  }
}

object Master{
  def main(args: Array[String]): Unit = {
    val host = args(0);
    val port = args(1)
    val configStr =
     s"""
        |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
        |akka.remote.netty.tcp.hostname = "$host"
        |akka.remote.netty.tcp.port = "$port"
      """.stripMargin

    val config: Config = ConfigFactory.parseString(configStr)

    val masterActorSystem = ActorSystem("masterActorSystem", config)
    masterActorSystem.actorOf(Props(new Master),"masterActor")
  }
}
