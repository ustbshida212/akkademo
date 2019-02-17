package com.itheima.rpc.sparkrpc

trait RemoteMessage extends Serializable{
}
//定义一个样例类,worker向master发送注册信息
case class RegisterMessage(workerId:String,memory:Int,cores:Int) extends RemoteMessage;
case class RegisteredMessage(message:String) extends RemoteMessage;
//worker向自己发送信息,在同一个进程中,不需要序列化
case object SendHeatBeat;
case class HeatBeat(workerId:String) extends RemoteMessage
//master向自己发送消息,在同一个进程中,不需要序列化
case object CheckTimeOut;