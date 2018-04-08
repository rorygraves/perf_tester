package org.perftester

import org.eclipse.jgit.revwalk.RevCommit

case class BranchRevision(index: Integer, revision: RevCommit) {
  def sha = revision.name()
}
