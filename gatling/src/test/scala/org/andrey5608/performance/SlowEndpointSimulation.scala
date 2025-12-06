package org.andrey5608.performance

import io.gatling.core.Predef._

class SlowEndpointSimulation extends Simulation {

  private val slowRequest = SimulationUtils.simpleGetRequest(
    requestName = "request_slow_endpoint",
    path = "/api/slow-response",
    expectedStatus = 200
  )

  private val slowScenarioPlan = SimulationUtils.buildScenario(
    scenarioName = "getSlowResponses",
    chain = slowRequest,
    targetRps = 20,
    rampUpSeconds = 60,
    durationSeconds = 240
  )

  setUp(slowScenarioPlan.population)
    .maxDuration(slowScenarioPlan.maxDuration)
    .assertions(
      details("request_slow_endpoint").successfulRequests.percent.gt(95.0),
      details("request_slow_endpoint").responseTime.max.lte(10000)
    )
}
