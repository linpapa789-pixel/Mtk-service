#pragma once

#include <Arduino.h>
#include "esp_timer.h"
#include "driver/gpio.h"

/**
 * Precision Hardware Glitch Generator for MediaTek BROM / Preloader Exploit
 * Generates calibrated ultra-short low/high pulses to trigger BootROM glitch window
 * on MT6765, MT6768, MT6762, MT6833, MT6877, etc.
 */
class GlitchEngine {
public:
    static void init(gpio_num_t pin = GPIO_NUM_4) {
        s_glitch_pin = pin;
        gpio_config_t io_conf = {};
        io_conf.intr_type = GPIO_INTR_DISABLE;
        io_conf.mode = GPIO_MODE_OUTPUT;
        io_conf.pin_bit_mask = (1ULL << s_glitch_pin);
        io_conf.pull_down_en = GPIO_PULLDOWN_DISABLE;
        io_conf.pull_up_en = GPIO_PULLUP_ENABLE;
        gpio_config(&io_conf);
        gpio_set_level(s_glitch_pin, 1); // Default HIGH (Pullup)
    }

    /**
     * Executes precision hardware glitch pulse
     * @param delay_us Microseconds delay after VBUS trigger
     * @param pulse_ns Pulse duration in nanoseconds (approximated via assembly loop or timer)
     */
    static IRAM_ATTR void triggerGlitch(uint32_t delay_us, uint32_t pulse_ns = 500) {
        if (delay_us > 0) {
            esp_rom_delay_us(delay_us);
        }

        // Drop line to GND (Glitch Active)
        GPIO.out_w1tc.val = (1UL << s_glitch_pin);

        // Nanosecond delay via direct assembly NOP loop (240MHz CPU clock -> 1 NOP ~ 4.16ns)
        uint32_t cycles = (pulse_ns * 240) / 1000;
        for (volatile uint32_t i = 0; i < cycles; i++) {
            asm volatile("nop");
        }

        // Restore line to HIGH
        GPIO.out_w1ts.val = (1UL << s_glitch_pin);
    }

    static void setGlitchPin(gpio_num_t pin) {
        s_glitch_pin = pin;
        init(pin);
    }

    static gpio_num_t getGlitchPin() {
        return s_glitch_pin;
    }

private:
    static gpio_num_t s_glitch_pin;
};

gpio_num_t GlitchEngine::s_glitch_pin = GPIO_NUM_4;
