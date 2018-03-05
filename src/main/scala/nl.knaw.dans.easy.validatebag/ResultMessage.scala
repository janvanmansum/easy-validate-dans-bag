/**
 * Copyright (C) 2018 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.validatebag

import java.net.URI

import nl.knaw.dans.easy.validatebag.InfoPackageType.InfoPackageType
import nl.knaw.dans.easy.validatebag.ValidationResult.ValidationResult
import org.json4s.ext.EnumNameSerializer
import org.json4s.native.Serialization._
import org.json4s.{ CustomSerializer, DefaultFormats, Formats, JNull, JString }


case class ResultMessage(
                          bagUri: URI,
                          bag: String,
                          //profileVersion TODO: first refactor so that this is detected before calling the validate function
                          infoPackageType: InfoPackageType,
                          result: ValidationResult,
                          ruleViolations: Option[Seq[(String, String)]] = None) {

  private implicit val formats: Formats =
    new DefaultFormats {} +
      new EnumNameSerializer(InfoPackageType) +
      new EnumNameSerializer(ValidationResult) +
      EncodingURISerializer

  def toJson(implicit pretty: Boolean = true): String = {
    if (pretty)
      writePretty(this)
    else
      write(this)
  }

  def toPlainText: String = {
    s"""
       |bag_uri: ${ bagUri.toASCIIString }
       |bag: $bag
       |info_package_type: $infoPackageType
       |result: $result
       |""".stripMargin ++ ruleViolations.map { violations =>
      "rule_violations:\n" +
        violations.map { case (nr, violation) => s" - [$nr] $violation" }.mkString("\n")
    }.getOrElse("")
  }
}

/*
 * Using a custom URI-serializer because the one from json4s extensions does not percent-encode.
 */
case object EncodingURISerializer extends CustomSerializer[URI](format => ( {
  case JString(s) => URI.create(s)
  case JNull => null
}, {
  case x: URI => JString(x.toASCIIString)
}))
