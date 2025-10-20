import { Router } from "express";
import { upload } from "../middlewares/multer.middleware.js";
import {uploadMedia} from "../controllers/user.conrollers.js"

const router = Router();
router.route("/uploadMedia").post(upload.single("media"),uploadMedia)
// app.get('/checks',(req,res)=>{
//     console.log("Yes")
//     res.status(200).json({
//         message:"ok"
//     })
// })

export default router;