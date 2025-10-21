import {v2 as cloudinary} from "cloudinary";
import fs from 'fs';
import mime from "mime-types";

cloudinary.config({ 
  cloud_name: process.env.CLOUDNINARY_CLOUD_NAME, 
  api_key: process.env.CLOUDNINARY_API_KEY, 
  api_secret: process.env.CLOUDNINARY_API_SECRET
});

const uploadOnCloudinary=async (localFilePath)=>{
    try{
        if(!localFilePath) return null
        const mimeType = mime.lookup(localFilePath);

        let resourceType = "raw"; 
        if (mimeType?.startsWith("image/")) resourceType = "image";
        else if (mimeType?.startsWith("video/")) resourceType = "video";
        else resourceType = "raw";

        // Upload
        const response=await cloudinary.uploader.upload(localFilePath,{
            resource_type: resourceType,
        })

        // If correctly Uploaded
        fs.unlinkSync(localFilePath)
        return response
    } catch(error){
        fs.unlinkSync(localFilePath)

        return null;
    }
}

export {uploadOnCloudinary}