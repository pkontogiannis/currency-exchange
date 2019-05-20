package com.landoop.utils

import scala.concurrent.{ExecutionContext, Future}

object TaskExtras {

  def toFutureOption[A](x: Option[Future[A]])(implicit ec: ExecutionContext): Future[Option[A]] =
    x match {
      case Some(f) => f.map(Some(_))
      case None => Future.successful(None)
    }

}
