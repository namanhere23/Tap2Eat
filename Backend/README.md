# Cloudinary Media Backend (Express.js)

This folder contains the Express.js backend responsible for handling media uploads (images and videos) directly to Cloudinary. It is structured as an API independent of the push notification service.

## 🛠️ Local Setup & Installation

Below are the steps to run this specific backend on your local machine.

### 1. Navigate to this Directory
Make sure your terminal is inside the **Cloudinary** backend directory:
```bash
cd Cloudinary_Backend
```

### 2. Install Dependencies
Install all required Node.js packages using npm:
```bash
npm install
```

### 3. Configure Environment Variables
Create a `.env` file **in the repository root** (the parent of `Cloudinary_Backend/`). Add the following variables:

```env
CORS_ORIGIN=<your-client-url>
CLOUDNINARY_CLOUD_NAME=<your-cloudinary-name>
CLOUDNINARY_API_KEY=<your-cloudinary-key>
CLOUDNINARY_API_SECRET=<your-cloudinary-secret>
PORT=8000
```
*(Replace the placeholders with your actual Cloudinary credentials).*

> **Note:** The env var names use `CLOUDNINARY` (not `CLOUDINARY`) — this matches the codebase. Use these exact names.

### 4. Run the Backend Server
Once the variables are configured, start the server:

**Development mode (with hot reload):**
```bash
npm run dev
```

**Production mode:**
```bash
npm start
```

> **Note:** `npm run dev` auto-loads `.env` via the `dotenv` CLI flag. With `npm start`, environment variables must be set in your shell or by your cloud provider — the `.env` file is **not** loaded automatically.

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET`  | `/checks` | Health-check — returns `{"message": "ok"}` |
| `POST` | `/api/v1/uploadMedia` | Upload a media file (image/video/document) to Cloudinary. Send as `multipart/form-data` with a `media` field. |

---

## 🚀 Individual Deployment Guide

To deploy this specific Node.js (Express) backend to a production service like **Render**, **Railway**, or **Vercel** independently of the FCM backend:

### Step 1: Configure Your Hosting Provider
Because this backend is located inside a sub-folder (`Cloudinary_Backend`), you must tell your hosting provider where to build and run the code.

- **Option A (Render/Railway):** In your Web Service settings, set the **"Root Directory"** to `Cloudinary_Backend`.
  - **Build Command:** `npm install`
  - **Start Command:** `npm start`

*(Note: If your hosting provider does not support root directory overrides, you might need to adjust your setup or deploy this subfolder uniquely using CLI tools).*

### Step 2: Set Environment Variables
Add your secrets securely to your provider's dashboard using the variables defined in step 3 above:
- `CORS_ORIGIN` (Your frontend's production URL)
- `CLOUDNINARY_CLOUD_NAME`
- `CLOUDNINARY_API_KEY`
- `CLOUDNINARY_API_SECRET`
- `PORT`

### Step 3: Deploy
Trigger a manual deployment. The server will start up running the Node server exclusively on the port given by your cloud provider.

### Step 4: Keep the Server Active (Render Free Tier)

Render's free tier spins down your service after **15 minutes of inactivity**. To prevent this, set up a free uptime monitor:

1. Sign up at [UptimeRobot](https://uptimerobot.com/) (free plan is sufficient).
2. Create a new **HTTP(s)** monitor.
3. Set the URL to your deployed health-check endpoint:

   https://<your-app>.onrender.com/checks

4. Set the monitoring interval to **every 5 minutes**.

This pings your server regularly so Render never puts it to sleep.