## This code is the source code implementation for the paper "DDLP Dynamic Location Data Publishing with Differential".



## Abstract

![](/pic/arc.png)

Mobile crowdsensing (MCS) has become an effective paradigm to facilitate urban sensing. However, mobile users participating in sensing tasks will face the risk of location privacy leakage when up loading their actual sensing location data. In the application of mobile crowdsensing, most location privacy protection studies do not consider the temporal correlations between locations, so they are vulnerable to various inference attacks, and there is the problem of low data availability. In order to solve the above problems, this paper proposes a dynamic differential location privacy data publishing framework (DDLP) that protects privacy while publishing locations continuously. Firstly, the corresponding Markov transition matrices are established according to different times of historical trajectories, and then the protection location set is generated based on the current location at each timestamp. Moreover, using the exponential mechanism in differential privacy perturbs the true location by designing the utility function. Finally, experiments on the real-world trajectory dataset show that our method not only provides strong privacy guarantees, but also outperforms existing methods in terms of data availability and computational efficiency



## Experimental Environment

**Operating environment：**

AMD Ryzen 7 5800H CPU and 16GB Memory,Ubuntu

**Installation：**

To run the code, you need to install the following packages：

```
commons-logging-1.2
geohash-1.3.0
javaoctave-0.7.1-20210221.133943-90
javatuples-1.2
opencsv-3.8
sqlite-jdbc-3.8.7
ujmp-complete-0.3.0
```

**Hyperparameters:**

- Training is conducted over 100 rounds with 10, 20, and 30 clients participating. Each client executes 4 epochs per round.
- The local learning rate is set at 0.01, and the batch size is 128.
- For CIFAR-10, clients' local data is limited to 4 classes to simulate heterogeneous settings. For CIFAR-100, clients' data is limited to 40 classes.
- The FedAR method uses a fairness factor qqq set to 0.1 and an attenuation factor ρ\rhoρ set to 0.5.

**Models:**

- ResNet-34 and MobileNet-V1 are used for evaluation.
  - **ResNet-34:** Consists of 34 convolutional layers, including 1 initial convolutional layer, 4 sets of residual blocks, a global average pooling layer, and a fully connected layer.
  - **MobileNet-V1:** Consists of sequence convolution and 1×1 convolution, including 13 depth-separable convolution layers, using 3×3 convolution and batch normalization followed by the ReLU activation function.

**Privacy-Preserving Methods:**

- **Differential Privacy (DP):** Noise is added to the data to protect user privacy while maintaining model accuracy.
- **Adaptive Privacy Budget Allocation:** Privacy budgets are allocated dynamically based on training progress to reduce the impact of noise on model updates and fairness.

**Evaluation Metrics:**

- **Model Performance:** The accuracy of local and global models is used as the primary metric.
- **Fairness:** Variance of the accuracy distribution of the global model on user local data is used to measure fairness.
- **Privacy Protection:** Privacy is assessed by ensuring the method satisfies differential privacy requirements.



### Implementation Details

The main algorithm implementation is done using MATLAB code, while Java code is used for data processing. Communication between Java and MATLAB code is facilitated using Octave.

- **Main Function:**
  - `Geolife_Demo`: The main function where JavaOctave is used to call the DDLP, PIM, and LM algorithms implemented in Octave.
- **Data Processing:**
  - `GeolifeDataAnalyse`: This function handles data processing, including the creation of the Markov transition matrix.
- **Geographical Space Discretization:**
  - `MapGrids` and `SquareGrids`: These functions are used for the discretization of the geographical space.
- **MATLAB Code:**
  - All `.m` files in the `matlab` folder contain the specific code for implementing the DDLP, PIM, and LM algorithms.



## Experimental Results

In the experiments conducted for this paper, we aimed to evaluate the performance and effectiveness of the proposed dynamic differential location privacy (DDLP) framework. The experiments were carried out using the real-world Geolife GPS Trajectories dataset, focusing on several key metrics such as the size of the protection location set (PLS), drift ratio, and the distance between true and perturbed locations.

1. **Size of PLS:** We measured how the size of the PLS changes over time and with different privacy budgets (ε) and privacy thresholds (θ). Our results showed that DDLP consistently produced a smaller PLS compared to existing methods, indicating better privacy protection.
2. **Drift Ratio:** The drift ratio was evaluated to understand how often the true location was excluded from the PLS. Our findings demonstrated that DDLP had the lowest drift ratio, suggesting more accurate location protection.
3. **Distance:** We measured the distance between the true location and the perturbed location to assess the utility of the perturbed data. DDLP produced the closest perturbed locations to the true locations, ensuring higher data utility.
4. **Running Time:** The computational efficiency of DDLP was also assessed, showing that it outperformed other methods in terms of running time, making it suitable for real-time applications.
5. **Transition Matrix at Different Times:** We analyzed the transition matrices for different times of the day (morning and afternoon) and found significant differences, validating the need for a dynamic transition matrix in our framework.

The experimental results confirm that the DDLP framework not only provides strong privacy guarantees but also enhances data utility and computational efficiency compared to existing methods.

![](/pic/1.png)

![](/pic/2.png)

![](/pic/3.png)

![](/pic/4.png)

![](/pic/5.png)

![](/pic/6.png)

![](/pic/7.png)

