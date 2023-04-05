# ClpsoPro
model:实例, 包含一个漆酶模型与力场  
psoem.sh: 自动化建模脚本  
xyz.shxiaj.pso: 最初版本的线性权重递减粒子群优化算法  
xyz.shxiaj.clpso: 后续改进的综合性学习的Pso  
xyz.shxiaj.clpsoChange: 增加位置限制, 不跑白不跑, 提高CPU利用率  
下一步改进: 增加对象序列化, 完善续跑功能, 增加其他运行结果参数的导出  
## 相关解释
https://shxiaj.xyz/post/matlab-clpso/  
https://shxiaj.xyz/post/java-pso/  

## 运行
```bash
javac -d bin ./java/xyz/shxiaj/clpsoChange/*
nohup java -cp bin xyz.shxiaj.clpsoChange.Pso &
```
