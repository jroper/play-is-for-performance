package controllers

import play.api.mvc._
import play.api.templates.{Template0, Html}
import views.html.slides

object Slides extends Controller {

  def slide(template: Template0[Html]) = Action(Ok(template.render()))

  def title = slide(slides.title)
  def yourHost = slide(slides.yourHost)

  def notAbout1 = slide(slides.notAbout1)
  def notAbout2 = slide(slides.notAbout2)

  def useCases1 = slide(slides.useCases1)
  def useCases2 = slide(slides.useCases2)
  def useCases3 = slide(slides.useCases3)
  def useCases4 = slide(slides.useCases4)

  def asyncVsSync1 = slide(slides.asyncVsSync1)
  def asyncVsSync2 = slide(slides.asyncVsSync2)
  def asyncVsSync3 = slide(slides.asyncVsSync3)

  def async1 = slide(slides.async1)
  def async2 = slide(slides.async2)

  def executionContexts1 = slide(slides.executionContexts1)
  def executionContexts2 = slide(slides.executionContexts2)
  def executionContexts3 = slide(slides.executionContexts3)
  def executionContexts4 = slide(slides.executionContexts4)

  def customContexts1 = slide(slides.customContexts1)
  def customContexts2 = slide(slides.customContexts2)
  def customContexts3 = slide(slides.customContexts3)
  def customContexts4 = slide(slides.customContexts4)
  def customContexts5 = slide(slides.customContexts5)
  def customContexts6 = slide(slides.customContexts6)
  def customContexts7 = slide(slides.customContexts7)
  def customContexts8 = slide(slides.customContexts8)

}
