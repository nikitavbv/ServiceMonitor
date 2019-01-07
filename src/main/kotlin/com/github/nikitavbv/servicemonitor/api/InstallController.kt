package com.github.nikitavbv.servicemonitor.api

import com.github.nikitavbv.servicemonitor.ApplicationProperties
import com.github.nikitavbv.servicemonitor.INSTALL_API
import org.apache.tomcat.util.http.fileupload.IOUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.FileInputStream
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping(INSTALL_API)
class InstallController (
    private val applicationProperties: ApplicationProperties
){

    @GetMapping()
    fun getScript(token: String): String {
        return """
            mkdir /sm
            curl ${applicationProperties.url}/agent-linux > /sm/agent
            curl ${applicationProperties.url}/install/config?token=$token > /sm/config.json
        """.trimIndent()
    }

    @GetMapping("/config")
    fun getConfig(token: String): String {
        return """
             {
                "backend": "${applicationProperties.url}",
                "projectToken": "$token",
                "monitor": [
                    {
                        "type": "memory"
                    },
                    {
                        "type": "io"
                    },
                    {
                        "type": "diskUsage"
                    },
                    {
                        "type": "cpu"
                    },
                    {
                        "type": "uptime"
                    },
                    {
                        "type": "network"
                    }
                ]
            }
        """.trimIndent()
    }

    @GetMapping("/agent")
    fun getAgent(response: HttpServletResponse) {
        val fileInputStream = FileInputStream("/app/agent")
        IOUtils.copy(fileInputStream, response.outputStream)
    }

}
