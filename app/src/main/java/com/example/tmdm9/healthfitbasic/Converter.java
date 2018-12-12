package com.example.tmdm9.healthfitbasic;

final class Converter {
    static float footToMeterConverter(float foot){
        return foot*.3048f;
    }

    static float meterToFootConverter(float meter){
        return meter/.3048f;
    }

    static float kilogramToPoundConverter(float kilogram){
        return kilogram*2.20462f;
    }

    static float poundToKilogramConverter(float pound){
        return pound/2.20462f;
    }
}
