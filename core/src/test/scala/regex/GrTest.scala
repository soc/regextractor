
//package scala.util.matching
package regex

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import maqicode.testing.JUnitTest
import maqicode.testing.speculum.inspect
//import scala.tools.testing.JUnitTest
//import scala.tools.testing.speculum._
import scala.reflect.ClassTag
import scala.PartialFunction.cond
import java.util.regex.PatternSyntaxException

//@RunWith(classOf[JUnit4])

class GrTest extends JUnitTest {

  //import Implicits._
  import regex.{ Mex => Gregex }

  @Test def gregularSimpleMatch(): Unit = {
    val r = """a(b)c""".gr
    "abc" match {
      case r(x) => inspect {
        "b" == x
      }
    }
  }
  @Test def gregularOptionalMatch(): Unit = {
    val r = """a(b)?c""".gr
    "abc" match {
      case r(Some(x)) => inspect {
        "b" == x
      }
    }
    "ac" match {
      case r(None) =>
      case _ => fail("ac")
    }
  }
  @Test def nestedGroups(): Unit = {
    val r = """a(xy(z))?c""".gr
    "axyzc" match {
      case r(Some(xyz), Some(z)) => inspect {
        "xyz" == xyz
        "z" == z
      }
    }
    "ac" match {
      case r(None, _) =>
      case _ => fail("Bad ac")
    }
    "axyzc" match {
      case gr"a${Some(xyz)}(xy${Some(z)}(z))?c" => inspect {
        "xyz" == xyz
        "z" == z
      }
    }
    "ac" match {
      case gr"a${Some(_)}(xy$_(z))?c" => fail("Bad ac")
      case gr"a$None(xy$_(z))?c" =>
      case _ => fail("Bad ac")
    }
  }
  @Test def nestedOptionalMatch(): Unit = {
    val r = """a(xy(z))?c""".gr
    "axyzc" match {
      case r(Some(xyz), Some(z)) => inspect {
        "xyz" == xyz
        "z" == z
      }
    }
  }
  @Test def nestedInNoncapturingOptional(): Unit = {
    val r = """a(?:xy(z))?c""".gr
    "axyzc" match {
      case r(Some(z)) => inspect {
        "z" == z
      }
    }
  }
  @Test def gregularRegexKnowsGroupName(): Unit = {
    val r = """a(?<boo>b)c""".gr
    val m = (r findFirstMatchIn "abc").get
    inspect {
      "b" == (m group "boo")
    }
  }
  @Test def gregularRegexKnowsGroupNames(): Unit = {
    val r = """a(?<boo>b)and(?<coo>c)d""".gr
    val m = (r findFirstMatchIn "abandcd").get
    inspect {
      "b" == (m group "boo")
      "c" == (m group "coo")
    }
  }
  @Test def gregularOptionalGroupIsOptional(): Unit = {
    val r = """a(b)and(c)?d""".gr
    "abandcd" match {
      case r(b, Some(c)) => inspect {
        ("b","c") == { (b,c) }
      }
      case _ => fail(s"$r didn't match optionally")
    }
    "abandd" match {
      case r(b, None) => inspect {
        "b" == b
      }
      case r(b, Some(c)) => fail(s"$r matched ($b, Some $c)")
      case _ => fail(s"$r didn't match None for empty group")
    }
  }

  @Test def itsAnInterpolator(): Unit = {
    val r = gr"""a(?<boo>b)c"""
    val m = (r findFirstMatchIn "abc").get
    inspect {
      "b" == (m group "boo")
    }
  }
  @Test def itCatchesBadPatterns(): Unit = {
    //gr"""a([])c"""  // DNC
  }
  val boob = 9
  @Test def itCatchesBadHoles(): Unit = {
    "anything" match {
      //case gr"""a(XYZ)c$boob""" => // DNC
      //case gr"""a(XYZ)c""" => // DNC
      case _ =>
    }
  }
  @Test def grUnapplies(): Unit = {
    "abandcd" match {
      case gr"""abandcd""" =>
      case _ => fail(s"gr didn't match literally")
    }
    "abandcd" match {
      case gr"""ab(?:and)cd""" =>
      case _ => fail(s"gr didn't match literally with non-capture")
    }
    "abandcd" match {
      case gr"""a$b(b)and${Some(c)}(c)?d""" => inspect {
        ("b","c") == { (b,c) }
      }
      case _ => fail(s"gr didn't match optionally")
    }
    "abandd" match {
      case gr"""a$b(b)and${Some(c)}(c)?d""" => fail(s"matched Some $c")
      case gr"""a$b(b)and${c_?}(c)?d""" => inspect {
        "b" == b
        None == c_?
      }
      case _ => fail(s"bad outcome")
    }
  }
  @Test def grPlainRegex(): Unit = {
    val prefix = "abc"
    val suffix = "xyz"
    val r = gr"$prefix(?:klm)$suffix" // s"$prefix(?:klm)$suffix".r
    val m = (r findFirstMatchIn "abcklmxyz").get
    inspect {
      "abcklmxyz" == m.matched
    }
    val g = gr"$prefix(?<k>klm)$suffix"
    val n = (g findFirstMatchIn "abcklmxyz").get
    inspect {
      "klm" == (n group "k")
    }
  }
  @Test def otherExtractions(): Unit = {
    import util.Try
    object ExInt {
      def unapply(s: String): Option[Int] = Try(s.toInt).toOption
    }
    val res = "123" match {
      case gr"""${ExInt(i)}(\d+)""" => i
      case _ => -1
    }
    inspect {
      123 == res
    }
  }
}
