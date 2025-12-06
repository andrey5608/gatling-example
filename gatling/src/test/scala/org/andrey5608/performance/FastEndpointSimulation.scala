package org.andrey5608.performance

import io.gatling.core.Predef._

class FastEndpointSimulation extends Simulation {

  private val fastRequest = SimulationUtils.simpleGetRequest(
    requestName = "request_fast_endpoint",
    path = "/api/fast-response",
    expectedStatus = 200
  )

  private val fastScenarioPlan = SimulationUtils.buildScenario(
    scenarioName = "getFastResponses",
    chain = fastRequest,
    targetRps = 60,
    rampUpSeconds = 60,
    durationSeconds = 180
  )

  setUp(fastScenarioPlan.population)
    .maxDuration(fastScenarioPlan.maxDuration)
    .assertions(
      details("request_fast_endpoint").successfulRequests.percent.gt(95.0),
      details("request_fast_endpoint").responseTime.max.lte(10000)
    )
}
