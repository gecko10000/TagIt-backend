package gecko10000.tagit.misc

import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream
import java.nio.file.Files

class Config {
    companion object {
        private val yaml = Yaml(LoaderOptions())
        private val defaultConfig: Map<String, Any>
        private val config: Map<String, Any>
        val DATA_DIRECTORY: String
        val PORT: Int

        private fun defaultConfigStream(): InputStream =
            ClassLoader.getSystemClassLoader().getResourceAsStream("config.yml")
                ?: throw InternalError("Could not open internal config for copying.")

        private fun saveDefaultConfig(file: File) {
            val inputStream = defaultConfigStream()
            Files.copy(inputStream, file.toPath())
        }

        private fun setupConfig(): Map<String, Any> {
            val configPath = System.getenv("TAGIT_CONFIG_PATH")?.ifEmpty { null } ?: "config.yml"
            val configFile = File(configPath)
            if (!configFile.exists()) {
                saveDefaultConfig(configFile)
            }
            return yaml.load(configFile.inputStream())
        }

        private fun <T> getOrDefault(key: String): T {
            return config[key] as T? ?: defaultConfig[key] as T
        }

        init {
            config = setupConfig()
            defaultConfig = yaml.load(defaultConfigStream())
            PORT = getOrDefault("port")
            DATA_DIRECTORY = getOrDefault("data_directory")
        }
    }
}
