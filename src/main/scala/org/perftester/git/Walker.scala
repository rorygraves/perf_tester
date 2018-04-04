package org.perftester.git

import org.eclipse.jgit.api.{Git, ListBranchCommand}
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object WalkerApp extends App {
  for (commit <- Walker.getRevisions("s:\\scala\\dan\\scala1\\.git",
                                     "refs/remotes/scala/2.12.x",
                                     "refs/heads/2.12.x_flag")) {
    println(s"${commit.getName} ${commit.getShortMessage} ${commit.getCommitterIdent.getName}")
  }
}

object Walker {

  def getRevisions(repoPath: String, base: String, test: String): List[RevCommit] = {
    val repository = new FileRepository(repoPath)
    val git        = new Git(repository)
    val bases      = new mutable.HashSet[RevCommit]
//    for (ref <- git.branchList.setListMode(ListBranchCommand.ListMode.ALL).call.asScala) {
//      println(ref)
//    }
    bases ++= git.log.add(repository.resolve(base)).call.asScala

    val it            = git.log.add(repository.resolve(test)).call.iterator.asScala
    val resultBuilder = ListBuffer[RevCommit]()

    @tailrec def add(): List[RevCommit] = {
      if (!it.hasNext) throw new Exception("no common commit")
      val next = it.next()
      resultBuilder += next
      if (bases.add(next)) add()
      else resultBuilder.result()
    }

    add()
  }

  //  def main(args: Array[String]): Unit = {
  //    try {
  //      val repository = new FileRepository("s:\\scala\\dan\\scala1\\.git")
  //      try {
  //        val git = new Git(repository)
  //        val base = "refs/remotes/scala/2.12.x"
  //        val branch = "refs/heads/2.12.x_flag"
  //        val bases = new util.HashSet[RevCommit]
  //        import scala.collection.JavaConversions._
  //        for (ref <- git.branchList.setListMode(ListBranchCommand.ListMode.ALL).call) {
  //          System.out.println(ref)
  //        }
  //        import scala.collection.JavaConversions._
  //        for (commit <- git.log.add(repository.resolve(base)).call) {
  //          bases.add(commit)
  //        }
  //        val it = git.log.add(repository.resolve(branch)).call.iterator
  //        while ( {
  //          it.hasNext
  //        }) {
  //          val commit = it.next
  //          System.out.println(commit.getName + " " + commit.getShortMessage + " " + commit.getCommitterIdent.getName)
  //          if (bases.contains(commit)) break //todo: break is not supported}
  //        }
  //        finally {
  //          if (repository != null) repository.close()
  //        }
  //      }
  //    }
}
