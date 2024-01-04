package pages.houseConsignment.index

import models.{Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.houseConsignment.HouseConsignmentSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object AddDepartureTransportMeansYesNoPage extends QuestionPage[Boolean] {

  override def path: JsPath = HouseConsignmentSection.path \ toString

  override def toString: String = "addDepartureTransportMeansYesNo"

  override def route(userAnswers: UserAnswers, departureId: String, mode: Mode): Option[Call] =
    Some(routes.AddDepartureTransportMeansYesNoController.onPageLoad(departureId, mode))

  // TODO  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = ???
}
