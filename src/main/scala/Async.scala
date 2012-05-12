package local.nodens.linkstorage
import local.nodens.linkmodel._
import android.os.{AsyncTask, Environment}
import java.lang.Void
import java.io.File
import android.util.Log
import scala.xml.{XML, Node}

import scalaz._
import Scalaz._

class DocLoader(activity:LinkStorageActivity) extends AsyncTask[AnyRef,Void,Validation[String, Document]] {
  def getArg(args:Array[AnyRef]):Validation[String, AnyRef] = validation(args.isEmpty either "could not get argument" or args(0))
  def getPath(arg:AnyRef):Validation[String, String] = validation(!arg.isInstanceOf[String] either "could not get path" or arg.asInstanceOf[String])
  def getDir:Validation[String, File] = Option(Environment.getExternalStorageDirectory()).fold(_.success, "could not get base directory".fail)
  def loadFile(file:File):Validation[String, Node] = try {
    XML.loadFile(file).success
  } catch {
    case t:Throwable => ("file at " + file.getCanonicalPath + " could not be loaded because " + t.getMessage).fail
  }
  def parseFile(node:Node):Validation[String, Document] = Document.parse(node).fold(_.success, "could not parse the xml".fail)

  override protected def doInBackground(args:AnyRef*) = for { //it's monadic!
    arg <- getArg(args.toArray)
    path <- getPath(arg)
    dir <- getDir
    file = new File(dir, path)
    loaded <- loadFile(file)
    doc <- parseFile(loaded)
  } yield doc

  override protected def onPostExecute(oDoc:Validation[String, Document]) = oDoc.fold(Log.e(TAG, _), activity.doc = _)
}

object DocLoader {
  def load(loc:String, activity:LinkStorageActivity) = {
    val dl = new DocLoader(activity)
    Log.d(TAG, "running execute now to load " + loc)
    dl.execute(loc)
  }
}

