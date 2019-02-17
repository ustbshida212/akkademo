package com.itheima.rpc.sparkrpc

class WorkerInfo(val workId:String,val memory:Int,val cores:Int){
  //用于存储worker上一次的心跳时间,因为每次都需要赋值改变,所以需要var修饰
  var lastHeartBeatTime:Long = 0;

  override def toString: String = {
    s"workerId:$workId,memory:$memory,cores:$cores";
  }
}
