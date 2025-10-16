import request from '@/utils/request'

/**
 * 上传文件
 */
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文件
 */
export function deleteFile(filename) {
  return request({
    url: `/file/${filename}`,
    method: 'delete'
  })
}

