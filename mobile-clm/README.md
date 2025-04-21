# Android App Setup Instructions

To successfully run this application, **your Android phone or emulator must be connected to the same local network (Wi-Fi)** as the PC running the backend server.

## 🔧 Setup Steps

1. **Find your PC's local IP address:**

    - On **Windows**:  
      Open a terminal and type:
      ```
      ipconfig
      ```
    - On **macOS / Linux**:  
      Open a terminal and type:
      ```
      ifconfig
      ```
    - Look for the IP address associated with your active network interface (e.g., something like `192.168.x.x`).

2. **Update the app with your PC's IP:**

    - Open `RetrofitClient.kt`  
      Replace the existing IP in the base URL with your PC's IP address.

    - Open `res/xml/network_security_config.xml`  
      Replace the IP there as well to match your PC's IP.

3. **Build and run the project.**

   If the app fails to fetch data, it may be due to your PC's firewall blocking incoming connections.

   👉 In that case, **temporarily disable your firewall**, or allow traffic on the port used by the backend (e.g., `8092`).

---

✅ Once everything is set up, launch the app — it should now be able to communicate with the backend!

