class CommonFunction {
  companion object Function2 {

      val str = CommonFunction.Function2.changeImages("3","1,2,3,4,5","과일,채소,야채,육류,과자")
      println("key val == $str") // 위의 두 줄은 다른 클래스에서 불러올때 사용하는 예시 구문임!
      
      fun changeImages(id: String, key: String, value: String): String {
          println("1")
          val keys = key.split(",").toTypedArray()
          println("2")
          val vals = value.split(",").toTypedArray()
          println("3")
          val map: MutableMap<String, Any> =
              ArrayMap() //객체 담는 컬렉션 배열
          println("4")
          for (i in vals.indices) {
              map[keys[i]] = vals[i]
          }
          println("5")
          println(map[id])
          println("6")
          print(map[id].toString())
          return map[id].toString()
      }
  }
}