package local.nodens.linkstorage
import local.nodens.linkmodel._
import android.os.{AsyncTask, Environment}
import java.lang.Void
import java.io.{File, InputStream}
import android.util.Log
import scala.xml.{XML, Node}
import scala.runtime.RichInt

import scalaz._
import Scalaz._

//everything needed to extract a single anyref argument as a proper type.
trait GetsArg {
  def getArg[A](args:Array[AnyRef])(implicit m:Manifest[A]):Validation[String, A] = extractArg(args) flatMap (convertArg[A](_)(m))
  def extractArg(args:Array[AnyRef]):Validation[String, AnyRef] = validation(args.isEmpty either "could not get argument" or args(0))
  def convertArg[A](arg:AnyRef)(implicit m:Manifest[A]):Validation[String, A] = {
    validation(!arg.isInstanceOf[A] either "could not extract arg as " + m.toString or arg.asInstanceOf[A])
  }
}

trait ParsesFile { def parseFile(node:Node):Validation[String, Document] = Document.parse(node).fold(_.success, "could not parse the xml".fail) }

trait PutsDoc {
  def activity:LinkStorageActivity
  def put(res:Loaded) = res.fold(Log.e(TAG, _), activity.doc = _)
}

trait GetsDir {
  def getDir:Validation[String, File] = Option(Environment.getExternalStorageDirectory()).fold(_.success, "could not get base directory".fail)
}

//abstract loader puts the loaded document into the activity inside display thread
class AbstractLoader(val activity:Activity) extends AsyncTask[AnyRef, Void, Loaded]
with GetsArg with ParsesFile with PutsDoc { override protected def onPostExecute(r:Loaded) = put(r) }

//load doc from the sdcard
class DocLoader(activity:LinkStorageActivity) extends AbstractLoader(activity) with GetsDir {
  def loadFile(file:File):Validation[String, Node] = try {
    XML.loadFile(file).success
  } catch {
    case t:Throwable => ("file at " + file.getCanonicalPath + " could not be loaded because " + t.getMessage).fail
  }
  override protected def doInBackground(args:AnyRef*) = for { //it's monadic!
    path <- getArg[String](args.toArray)
    dir <- getDir
    file = new File(dir, path)
    loaded <- loadFile(file)
    doc <- parseFile(loaded)
  } yield doc
}

//load doc from raw resources.
class RawDocLoader(activity:LinkStorageActivity) extends AbstractLoader(activity) {
  def loadStream(id:Int):Validation[String, InputStream] = try {
    activity.getResources.openRawResource(id).success
  } catch {
    case t:Throwable => ("could not load resource " + id + " because " + t.getMessage).fail
  }
  def loadXML(is:InputStream):Validation[String, Node] = try {
    XML.load(is).success
  } catch {
    case t:Throwable => ("could not load xml because " + t.getMessage).fail
  }
  override protected def doInBackground(args:AnyRef*):Loaded = for {
    id <- getArg[RichInt](args.toArray)
    stream <- loadStream(id.toInt)
    xml <- loadXML(stream)
    doc <- parseFile(xml)
  } yield doc
}

//object w/ static methods to load stuff
object DocLoader {
  def load(loc:String, activity:LinkStorageActivity) = {
    val dl = new DocLoader(activity)
    dl.execute(loc)
  }
  def loadRaw(id:Int, activity:LinkStorageActivity) = {
    val dl = new RawDocLoader(activity)
    dl.execute(new RichInt(id))
  }
}

