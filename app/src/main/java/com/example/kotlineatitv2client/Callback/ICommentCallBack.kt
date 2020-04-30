package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Model.CategoryModel
import com.example.kotlineatitv2client.Model.CommentModel

interface ICommentCallBack {
    fun onCommentLoadSuccess(commentList:List<CommentModel>)
    fun onCommentLoadFailed(message:String)
}