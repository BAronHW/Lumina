package com.example.lumina.Domain

case class Pagination(page: Int, pageSize: Int):
  def limit: Int = pageSize
  def offset: Int = (page - 1) * pageSize
