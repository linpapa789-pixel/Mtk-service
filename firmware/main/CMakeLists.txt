idf_component_register(SRCS "main.cpp"
                    INCLUDE_DIRS "." "../src"
                    REQUIRES esp_wifi esp_event nvs_flash driver esp_timer)
