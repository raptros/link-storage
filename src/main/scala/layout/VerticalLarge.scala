package local.nodens.linkstorage.layout
import local.nodens.linkstorage._

import android.app._
import android.os.Bundle
import android.view._
import android.widget._
import android.util.Log
import local.nodens.linkmodel._
import android.content._
import ActionBar.{Tab, TabListener}

import scalaz._
import Scalaz._



class VerticalLarge(activity:LinkStorageActivity) extends LayoutMgr(activity) {
  def prepareView:View = {
    null
  }
  def displayDoc(doc:Document):Unit = { } 
  def displaySection(sec:SecStackItem):Unit = { }

  def restoreInstanceState(sis:Option[Bundle]):Unit = { }
  def onSaveInstanceState(outSIS:Bundle):Unit = { }
}
