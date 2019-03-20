package io.sam.main

import scala.util.control.NonFatal

object manage {
	def apply[R <: {def close(): Unit}, T](resource: => R)(f: R => T): T = {
		var res: Option[R] = None
		try{
			res = Some(resource)
			f(res.get)
		}catch {
			case NonFatal(ex) => throw ex
		}finally {
			if (res.isDefined)
				res.get.close()
		}
	}
}
