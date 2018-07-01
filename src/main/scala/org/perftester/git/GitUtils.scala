package org.perftester.git

import java.io.File

import ammonite.ops.Path
import org.apache.commons.logging.LogFactory
import org.eclipse.jgit.api.CherryPickResult.CherryPickStatus
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.api.{Git, ListBranchCommand}
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.RemoteConfig

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object WalkerApp extends App {
  for (commit <- GitUtils("s:\\scala\\dan\\scala1").branchRevisions("refs/remotes/scala/2.12.x",
                                                                    "refs/heads/2.12.x_flag")) {
    println(s"${commit.getName} ${commit.getShortMessage} ${commit.getCommitterIdent.getName}")
  }
}

object GitUtils {
  def apply(repoPath: File): GitUtils = {
    if (!repoPath.exists()) {
      throw new IllegalStateException(s"git repo does not exist - $repoPath")
    }

    if (!repoPath.toString.endsWith(".git"))
      apply(new File(repoPath, ".git"))
    else new GitUtils(repoPath)
  }

  def apply(repoPath: Path): GitUtils = GitUtils(repoPath.toIO)

  def apply(repoPath: String): GitUtils = GitUtils(new File(repoPath))
}

class GitUtils private (repoPath: File) {
  val logger = LogFactory.getLog(s"GitUtils($repoPath)")
  logger.info(s"using git in repo $repoPath")

  val repository = new FileRepository(repoPath)
  val git        = new Git(repository)

  def branches = {
    git.branchList.setListMode(ListBranchCommand.ListMode.ALL).call.asScala.toList
  }

  def dispose(): Unit = git.close()

  def resolveBranch(msg: String, branchSpec: String) = {
    val res = repository.resolve(branchSpec)
    if (res eq null) throw new IllegalStateException(s"cannot resolve $branchSpec - $msg")
    res
  }

  def branchRevisions(baseBranch: String, myBranch: String): List[RevCommit] = {
    val bases = git.log.add(resolveBranch("base", baseBranch)).call.asScala.toSet

    val it            = git.log.add(resolveBranch("branch", myBranch)).call.iterator.asScala
    val resultBuilder = ListBuffer[RevCommit]()

    @tailrec def add(): List[RevCommit] = {
      if (!it.hasNext) throw new Exception("no common commit")
      val next = it.next()
      resultBuilder += next
      if (!bases.contains(next)) add()
      else resultBuilder.result()
    }

    add().reverse
  }

  def remotes: Map[String, RemoteConfig] =
    git
      .remoteList()
      .call()
      .asScala
      .map {
        case remote => remote.getName -> remote
      }(collection.breakOut)

  def fetchAll(): Unit = {
    remotes.values foreach fetch
  }

  def fetch(remote: String): Unit = {
    fetch(remotes(remote))
  }

  def fetch(remote: RemoteConfig): Unit = {
    logger.info(s"fetching ${remote.getName}")
    git
      .fetch()
      .setRemoveDeletedRefs(true) // -prune
      .setRemote(remote.getName)
      .setRefSpecs(remote.getFetchRefSpecs)
      .setDryRun(false)
      .setCheckFetchedObjects(true)
      .call()
  }

  def resetToRevision(rev: String): Unit = {
    logger.info(s"Reset to revision: $rev")
    logger.info(s"Executing git reset --hard $rev")

    // git reset --hard $rev
    git
      .reset()
      .setMode(ResetType.HARD)
      .setRef(rev)
      .call()

    // git clean -fxd
    // scala build can sometimes break if, for example, switching from 2.12.x to 2.13.x
    logger.info(s"Executing git reset --hard $rev")
    git.clean().setForce(true).setIgnore(false).setCleanDirectories(true).call()
  }

  def cherryPick(sha: String): Unit = cherryPick(repository.resolve(sha))

  def cherryPick(sha: AnyObjectId): Unit = {
    logger.info(s"git cherry-pick $sha")
    val res = git
      .cherryPick()
      .include(sha)
      .call()
    res match {
      case CherryPickStatus.OK =>
        logger.info("Cherry pick successful")
      case _ =>
        logger.error("Cherry pick failed: " + res.getStatus)
        throw new IllegalStateException(s"Failed to perform cherry-pick of $sha")
    }
  }
}
