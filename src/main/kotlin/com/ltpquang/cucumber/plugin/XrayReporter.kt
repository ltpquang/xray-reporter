package com.ltpquang.cucumber.plugin

import com.ltpquang.xray.client.XrayClient
import com.ltpquang.xray.models.Status
import io.cucumber.plugin.ConcurrentEventListener
import io.cucumber.plugin.event.EventPublisher
import io.cucumber.plugin.event.TestCaseEvent
import io.cucumber.plugin.event.TestCaseFinished
import io.cucumber.plugin.event.TestCaseStarted
import java.net.URL

/**
 * Created by Quang Le (quangltp) on 7/25/20
 *
 */

class XrayReporter(url: URL) : ConcurrentEventListener {
    private val xrayClient = XrayClient(
            "${url.protocol}://${url.host}",
            url.userInfo.split(":")[0],
            url.userInfo.split(":")[1])

    override fun setEventPublisher(publisher: EventPublisher?) {
        publisher?.registerHandlerFor(TestCaseStarted::class.java, this::handleTestCaseStarted)
        publisher?.registerHandlerFor(TestCaseFinished::class.java, this::handleTestCaseFinished)
    }

    private fun handleTestCaseStarted(event: TestCaseStarted) {
        val (testExecKey, testKey) = extractIssues(event) ?: return
        xrayClient.setStatus(testKey, testExecKey, Status.EXECUTING)
    }

    private fun handleTestCaseFinished(event: TestCaseFinished) {
        val (testExecKey, testKey) = extractIssues(event) ?: return
        val resolvedStatus = resolveStatus(event.result?.status!!)
        xrayClient.setStatus(testKey, testExecKey, resolvedStatus)
    }

    // Test Execution Issue Key - Test Issue Key
    private fun extractIssues(event: TestCaseEvent): Pair<String, String>? {
        val tags = event.testCase.tags
        if (tags.isEmpty() || tags.size < 2) {
            return null
        }
        return Pair(tags[0].removePrefix("@"), tags[1].removePrefix("@"))
    }

    private fun resolveStatus(status: io.cucumber.plugin.event.Status): Status =
            when (status) {
                io.cucumber.plugin.event.Status.PASSED -> Status.PASS
                io.cucumber.plugin.event.Status.FAILED -> Status.FAIL
                else -> Status.TODO
            }
}
