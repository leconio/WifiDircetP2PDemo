package com.leconio.wifidircet

enum class Car {
    AUDI,BWM,BENZ
}

enum class Car2(val maxSpeend:Int) {
    AUDI(200),BWM(210),BENZ(220)
}

sealed class Car3(val maxSpeed:Int) {

    object AUDI:Car3(200)
    object BWM:Car3(210)
    object BENZ:Car3(220)

    class CustomCar1(val m:Int) :Car3(DEFAULT_MAX_SPEED)
    class CustomCar2(val m:Int) :Car3(DEFAULT_MAX_SPEED)

    companion object {
        const val DEFAULT_MAX_SPEED = 200
    }

}

fun race(car:Car3) {
    when(car) {
        Car3.AUDI -> println ("AUDI max_speed is " + Car3.AUDI.maxSpeed)
        Car3.BWM -> println("BWM max_speed is " + Car3.BWM.maxSpeed)
        Car3.BENZ -> println("BENZ max_speed is " + Car3.BENZ.maxSpeed)
        is Car3.CustomCar1 -> println("CustomCar1 max_speed is " + (car.maxSpeed + car.m))
        is Car3.CustomCar2 -> println("CustomCar2 max_speed is " + (car.maxSpeed + car.m))
        else -> Car3.DEFAULT_MAX_SPEED
    }
}

fun main() {
    race(Car3.AUDI)
    race(Car3.BENZ)
    race(Car3.CustomCar1(30))
}