import asyncHandler from "../utils/asyncHandler.js"
import {ApiError} from "../utils/ApiError.js"
import {uploadOnCloudinary} from "../utils/cloudinary.js"
import {ApiResponse} from '../utils/ApiRespose.js'
import { upload } from "../middlewares/multer.middleware.js"

const uploadMedia=asyncHandler(async(req,res)=>{
    const localFilePath=req.file?.path

    if(!localFilePath){
        throw new ApiError(400,"Media Missing")
    }

    const cloudinaryMedia=await uploadOnCloudinary(localFilePath)
    if(!cloudinaryMedia){
        throw new ApiError(400,"Error while Uploading")
    }

    return res.status(200)
    .json(
        new ApiResponse(200,cloudinaryMedia)
    )
})

export {uploadMedia}