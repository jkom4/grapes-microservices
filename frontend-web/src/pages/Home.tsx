import React from "react";
import HeaderSection from "../sections/homepage/HeaderSection";
import PopularSection from "../sections/homepage/PopularSection";
import UseSection from "../sections/homepage/UseSection";
import AboutUsSection from "../sections/homepage/AboutUsSection";
import DisplayProductSection from "../sections/homepage/DisplayProductSection";
import WhoAreWeSection from "../sections/homepage/WhoAreWeSection";
import Newsletter from "../sections/homepage/Newsletter";


// MainPage Component: Combines all sections into the main page
export default function MainPage() {
    return (
        <>
            <HeaderSection />
            <PopularSection />
            <UseSection />
            <AboutUsSection />
            <DisplayProductSection />
            <WhoAreWeSection />
            <Newsletter />
        </>
    );
}