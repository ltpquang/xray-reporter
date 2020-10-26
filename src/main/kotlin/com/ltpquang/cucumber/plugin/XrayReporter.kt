package com.ltpquang.cucumber.plugin

import com.ltpquang.xray.client.XrayClient
import com.ltpquang.xray.models.Status
import io.cucumber.gherkin.GherkinDocumentBuilder
import io.cucumber.gherkin.Parser
import io.cucumber.messages.Messages
import io.cucumber.plugin.ConcurrentEventListener
import io.cucumber.plugin.event.*
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
    private var featureTags: List<String> = ArrayList()

    override fun setEventPublisher(publisher: EventPublisher?) {
        publisher?.registerHandlerFor(TestSourceRead::class.java, this::handleTestSourceRead)
        publisher?.registerHandlerFor(TestCaseStarted::class.java, this::handleTestCaseStarted)
        publisher?.registerHandlerFor(TestCaseFinished::class.java, this::handleTestCaseFinished)
    }

    private fun handleTestSourceRead(event: TestSourceRead) {
        val parser: Parser<Messages.GherkinDocument.Builder> = Parser(
            GherkinDocumentBuilder { "" })
        val builder: Messages.GherkinDocument.Builder = parser.parse(event.source)
        val doc: Messages.GherkinDocument = builder.build()
        val tagList = doc.feature?.tagsList
        featureTags = tagList?.mapNotNull { it.name }!!
    }

    private fun handleTestCaseStarted(event: TestCaseStarted) {
        val testKey = getTestKey(event)
        val testExecKey = getTestExecutionKey()
        if (testKey.isEmpty() || testExecKey.isEmpty()) {
            println("skip reporting")
            return
        }
        xrayClient.setStatus(testKey, testExecKey, Status.EXECUTING)
    }

    private fun handleTestCaseFinished(event: TestCaseFinished) {
        val testKey = getTestKey(event)
        val testExecKey = getTestExecutionKey()
        val resolvedStatus = resolveStatus(event.result?.status!!)
        if (testKey.isEmpty() || testExecKey.isEmpty()) {
            println("skip reporting")
            return
        }
        xrayClient.setStatus(testKey, testExecKey, resolvedStatus)
    }

    private fun getTestExecutionKey(): String {
        if (featureTags.isEmpty()) {
            println("WARNING: Empty feature tag")
            return ""
        }
        return featureTags[0].removePrefix("@")
    }

    private fun getTestKey(event: TestCaseEvent): String {
        val tags = event.testCase.tags.filter { !featureTags.contains(it) }
        if (tags.isEmpty()) {
            println("WARNING: Empty scenario tag")
            return ""
        }
        return tags[0].removePrefix("@")
    }

    private fun resolveStatus(status: io.cucumber.plugin.event.Status): Status =
            when (status) {
                io.cucumber.plugin.event.Status.PASSED -> Status.PASS
                io.cucumber.plugin.event.Status.FAILED -> Status.FAIL
                else -> Status.TODO
            }
}
