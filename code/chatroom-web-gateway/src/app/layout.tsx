import UserProvider from "@/contexts/UserContext";
import "./globals.css";
import localFont from "next/font/local";

const calibre = localFont({
    src: [
        {path: "../../public/fonts/CalibreRegular.otf", weight: "normal"},
        {path: "../../public/fonts/CalibreMedium.otf", weight: "500"},
        {path: "../../public/fonts/CalibreLight.otf", weight: "300"},
        {path: "../../public/fonts/CalibreSemibold.otf", weight: "600"},
        {path: "../../public/fonts/CalibreBold.otf", weight: "bold"},
    ],
});

export default function RootLayout({children}: { children: React.ReactNode; }) {

    return (
        <html lang="en">
        <UserProvider>
            <body className={calibre.className}>{children}</body>
        </UserProvider>
        </html>
    );
}
