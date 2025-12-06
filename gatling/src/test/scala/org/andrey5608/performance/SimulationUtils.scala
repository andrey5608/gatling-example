package org.andrey5608.performance

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, PopulationBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import scala.concurrent.duration._

object SimulationUtils {

  final case class ScenarioPlan(population: PopulationBuilder, maxDuration: FiniteDuration)

  private val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .userAgentHeader("gatling-example")

  def simpleGetRequest(requestName: String, path: String, expectedStatus: Int): ChainBuilder = {
    exec(
      http(requestName)
        .get(path)
        .check(status.is(expectedStatus))
    )
  }

  def buildScenario(
      scenarioName: String,
      chain: ChainBuilder,
      targetRps: Int,
      rampUpSeconds: Int,
      durationSeconds: Int
  ): ScenarioPlan = {
    val scenarioBuilder: ScenarioBuilder = scenario(scenarioName)
      .forever(
        pace(1.seconds)
          .exec(chain)
      )

    val population: PopulationBuilder = scenarioBuilder
      .inject(
        rampConcurrentUsers(1).to(targetRps).during(rampUpSeconds.seconds),
        constantConcurrentUsers(targetRps).during(durationSeconds.seconds)
      )
      .protocols(httpProtocol)

    val totalDuration = (rampUpSeconds + durationSeconds).seconds
    ScenarioPlan(population = population, maxDuration = totalDuration)
  }
}
