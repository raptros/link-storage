package local.nodens.linkstorage.data
import local.nodens.linkstorage._
import local.nodens.linkmodel._

import android.app._
import android.view._
import android.widget._
import android.os.Bundle
import android.util.{AttributeSet, TypedValue}
import android.content.Context

import scalaz._
import Scalaz._
import Gravity.{LEFT, RIGHT, CENTER_HORIZONTAL, CENTER_VERTICAL}
import TypedValue.COMPLEX_UNIT_DIP
import LinearLayout.LayoutParams
import ViewGroup.LayoutParams.{MATCH_PARENT, WRAP_CONTENT}

class LinkItem(val context:Context, attrSet:AttributeSet) extends LinearLayout(context, attrSet) {
  def getText(id:Int):Option[TextView] = {
    val v = findViewById(id)
    if (v == null || !v.isInstanceOf[TextView]) None else  Some(v.asInstanceOf[TextView])
  }

  def update(title:String, url:String) = {
    getText(R.id.link_title_view) foreach (_.setText(title))
    getText(R.id.link_url_view) foreach (_.setText(url))
    this
  }
}

object LinkItem {
  def apply(context:Context, linkTitle:String, linkUrl:String) = {
    val inflater = LayoutInflater.from(context)
    val li = inflater.inflate(R.layout.link_item, null).asInstanceOf[LinkItem]
    li.update(linkTitle, linkUrl)
  }
}

class SeparatorItem(val context:Context, attrSet:AttributeSet) extends TextView(context, attrSet) {
  def update(newIdx:Int) = {
    setText(newIdx.toString)
    this
  }
}

object SeparatorItem {
  def apply(context:Context, idx:Int) = {
    val inflater = LayoutInflater.from(context)
    val si = inflater.inflate(R.layout.separator_item, null).asInstanceOf[SeparatorItem]
    si.update(idx)
  }
}

class SectionItem(val context:Context, attrSet:AttributeSet) extends TextView(context, attrSet) {
  def update(newSec:String) = {
    setText(newSec)
    this
  }
}

object SectionItem {
  def apply(context:Context, section:String) = {
    val inflater = LayoutInflater.from(context)
    val si = inflater.inflate(R.layout.section_item, null).asInstanceOf[SectionItem]
    si.update(section)
  }
}

class LLSHeaderItem(val context:Context, attrSet:AttributeSet) extends TextView(context, attrSet) 

object LLSHeaderItem {
  def apply(context:Context, title:String) = {
    val inflater = LayoutInflater.from(context)
    val lhi = inflater.inflate(R.layout.lls_header_item, null).asInstanceOf[LLSHeaderItem]
    lhi.setText(title)
    lhi
  }
}
