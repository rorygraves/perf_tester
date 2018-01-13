package org.perftester.results.rows

sealed trait DataRow {
  def rowType: DataRowType[_ <: DataRow]

  def startNs: Long

  def endNs: Long

  def duration = endNs - startNs
}

sealed trait PhaseRow extends DataRow {
  def phaseId: Int

  def phaseName: String

  def purpose: String

  def threadId: Int

  def threadName: String

  def runNs: Long

  def idleNs: Long

  def cpuTimeNs: Long

  def userTimeNs: Long

  def allocatedBytes: Long

  def heapSize: Long
//
//  override def startMs = startNs / 1000000
//
//  override def endMs = endNs / 1000000
//
//  def runMs = runNs / 1000000
//
//  def idleMs = idleNs / 1000000
//
//  def cpuTimeMs = cpuTimeNs / 1000000
//
//  def userTimeMs = userTimeNs / 1000000
//
//  def allocatedMbs = allocatedBytes.toDouble / (1024 * 1024)
//
//  def heapSizeMb = heapSize.toDouble / (1024 * 1024)

}

case object DataRowType {
  val allTypes: Map[String, DataRowType[_ <: DataRow]] =
    List(GCDataRowType, MainDataRowType, BackgroundDataRowType).map { v =>
      v.typeStr -> v
    }(scala.collection.breakOut)
}

sealed abstract class DataRowType[T <: DataRow](val typeStr: String) {
  override def toString: String = s"Type[$typeStr]"

  def parse(values: List[String]): T
}

case object MainDataRowType extends DataRowType[MainPhaseRow]("main") {
  //header(main/background),startNs,endNs,runId,phaseId,phaseName,purpose,threadId,threadName,runNs,idleNs,cpuTimeNs,userTimeNs,allocatedByte,heapSize
  //main,357709788004284,357709794618424,1,3,packageobjects,,105,pool-12-thread-2,6614140,0,6286000,6211000,274872,294563408
  override def parse(values: List[String]) =
    MainPhaseRow(
      values(1).toLong, // startNs
      values(2).toLong, // endNs
      values(3).toInt, //  runId
      values(4).toInt, // phaseId
      values(5), // phaseName
      values(6), // purpose
      values(7).toInt, // threadId
      values(8), // threadName
      values(9).toLong, // runNs
      values(10).toLong, // idleNs
      values(11).toLong, // cpuTimeNs
      values(12).toLong, // userTimeNs
      values(13).toLong, // allocatedBytes
      values(14).toLong // heapSize
    )
}

case object BackgroundDataRowType extends DataRowType[BackgroundPhaseRow]("background") {
  //header(main/background),startNs,endNs,runId,phaseId,phaseName,purpose,threadId,threadName,runNs,idleNs,cpuTimeNs,userTimeNs,allocatedByte,heapSize
  //main,357709788004284,357709794618424,1,3,packageobjects,,105,pool-12-thread-2,6614140,0,6286000,6211000,274872,294563408
  override def parse(values: List[String]) =
    BackgroundPhaseRow(
      values(1).toLong, // startNs
      values(2).toLong, // endNs
      values(3).toInt, //  runId
      values(4).toInt, // phaseId
      values(5), // phaseName
      values(6), // purpose
      values(7).toInt, // threadId
      values(8), // threadName
      values(9).toLong, // runNs
      values(10).toLong, // idleNs
      values(11).toLong, // cpuTimeNs
      values(12).toLong, // userTimeNs
      values(13).toLong, // allocatedBytes
      values(14).toLong // heapSize
    )
}

case object GCDataRowType extends DataRowType[GCDataRow]("GC") {
  override def parse(values: List[String]) =
    //header(GC),startNs,endNs,startMs,endMs,name,action,cause,threads
    GCDataRow(
      values(1).trim.toLong, // startNs
      values(2).trim.toLong, // endNs
      values(3).trim.toLong, // startMs
      values(4).trim.toLong, // endMs
      values(5), // name
      values(6), // action
      values(7), // cause
      values(8).trim.toInt // threads
    )
}

case class GCDataRow(startNs: Long,
                     endNs: Long,
                     startMs: Long,
                     endMs: Long,
                     name: String,
                     action: String,
                     cause: String,
                     threads: Int)
    extends DataRow {
  override def rowType = GCDataRowType
}

//header(main/background),startNs,endNs,runId,phaseId,phaseName,purpose,threadId,threadName,runNs,idleNs,cpuTimeNs,userTimeNs,allocatedByte,heapSize
//main,357709788004284,357709794618424,1,3,packageobjects,,105,pool-12-thread-2,6614140,0,6286000,6211000,274872,294563408
//header(GC),startNs,endNs,startMs,endMs,name,action,cause,threads
//GC,357711549518405,357711606518405,28247, 28303,PS Scavenge,end of minor GC,Allocation Failure,8
case class MainPhaseRow(startNs: Long,
                        endNs: Long,
                        runId: Int,
                        phaseId: Int,
                        phaseName: String,
                        purpose: String,
                        threadId: Int,
                        threadName: String,
                        runNs: Long,
                        idleNs: Long,
                        cpuTimeNs: Long,
                        userTimeNs: Long,
                        allocatedBytes: Long,
                        heapSize: Long)
    extends PhaseRow {
  override def rowType: DataRowType[MainPhaseRow] = MainDataRowType
}

case class BackgroundPhaseRow(startNs: Long,
                              endNs: Long,
                              runId: Int,
                              phaseId: Int,
                              phaseName: String,
                              purpose: String,
                              threadId: Int,
                              threadName: String,
                              runNs: Long,
                              idleNs: Long,
                              cpuTimeNs: Long,
                              userTimeNs: Long,
                              allocatedBytes: Long,
                              heapSize: Long)
    extends PhaseRow {
  override def rowType: DataRowType[BackgroundPhaseRow] = BackgroundDataRowType
}

object DataRow {
  def apply(values: List[String]): Option[DataRow] = {
    values(0) match {
      case "info" =>
        None
      case x if x.startsWith("header(") =>
        None
      case x if DataRowType.allTypes.contains(x) =>
        Some(DataRowType.allTypes(x).parse(values))
      case x =>
        println(s"NO MATCH $x")
        None
    }
  }
}
