package com.dragonguard.alert.email

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class EmailConsumer(
    private val redisMessageListenerContainer: RedisMessageListenerContainer,
    private val javaMailSender: JavaMailSender,
    private val objectMapper: ObjectMapper,
) : MessageListener {

    private val logger = LoggerFactory.getLogger(EmailConsumer::class.java)

    @PostConstruct
    fun init() {
        val topic = ChannelTopic("alert:email")
        redisMessageListenerContainer.addMessageListener(this, topic)
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        try {
            val payload = String(message.body)
            val request = objectMapper.readValue(payload, EmailRequest::class.java)
            sendVerificationEmail(request.email, request.code)
        } catch (e: Exception) {
            logger.error("이메일 발송 중 오류 발생: {}", e.message)
        }
    }

    private fun sendVerificationEmail(memberEmail: String, code: Int) {
        val mimeMessage = javaMailSender.createMimeMessage()
        val mimeMessageHelper = MimeMessageHelper(mimeMessage, false, "UTF-8")

        mimeMessageHelper.setTo(memberEmail)
        mimeMessageHelper.setSubject("GitRank 조직 인증")
        mimeMessageHelper.setText(createEmailContent(code), true)

        javaMailSender.send(mimeMessage)
    }

    private fun createEmailContent(code: Int): String =
        """
        <html>
        <head></head>
        <body>
            <div>다음 번호를 입력해주세요:</div>
            <div><h1>$code</h1></div>
        </body>
        </html>
        """.trimIndent()
}
